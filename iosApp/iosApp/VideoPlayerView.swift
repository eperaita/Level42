import SwiftUI
import AVKit

struct VideoPlayerView: UIViewControllerRepresentable {
    var videoName: String
    var onVideoFinished: () -> Void

    func makeUIViewController(context: Context) -> AVPlayerViewController {
        let controller = AVPlayerViewController()
        controller.showsPlaybackControls = false

        if let url = Bundle.main.url(forResource: videoName, withExtension: "mp4") {
            let player = AVPlayer(url: url)
            controller.player = player
            controller.videoGravity = .resizeAspectFill

            NotificationCenter.default.addObserver(
                context.coordinator,
                selector: #selector(Coordinator.videoDidFinish),
                name: .AVPlayerItemDidPlayToEndTime,
                object: player.currentItem
            )

            player.play()
        }

        return controller
    }

    func updateUIViewController(_ uiViewController: AVPlayerViewController, context: Context) {}

    func makeCoordinator() -> Coordinator {
        Coordinator(onVideoFinished: onVideoFinished)
    }

    class Coordinator: NSObject {
        var onVideoFinished: () -> Void

        init(onVideoFinished: @escaping () -> Void) {
            self.onVideoFinished = onVideoFinished
        }

        @objc func videoDidFinish() {
            onVideoFinished()
        }
    }
}

struct VideoPlayerWrapper: View {
    var videoName: String
    var onVideoFinished: () -> Void

    var body: some View {
        VideoPlayerView(
            videoName: videoName,
            onVideoFinished: onVideoFinished
        )
        .edgesIgnoringSafeArea(.all)
    }
}